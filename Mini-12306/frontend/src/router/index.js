import { createRouter, createWebHistory } from 'vue-router';
import TicketSearch from '../views/TicketSearch.vue';
import TicketRefund from '../views/TicketRefund.vue';
import UserTickets from '../views/UserTickets.vue';

const routes = [
  {
    path: '/',
    redirect: '/tickets/search'
  },
  {
    path: '/tickets/search',
    name: 'TicketSearch',
    component: TicketSearch
  },
  {
    path: '/tickets/refund',
    name: 'TicketRefund',
    component: TicketRefund
  },
  {
    path: '/tickets/user',
    name: 'UserTickets',
    component: UserTickets
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;
